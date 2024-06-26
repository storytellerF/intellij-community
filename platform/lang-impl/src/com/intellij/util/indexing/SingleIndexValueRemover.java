// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.util.indexing;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.util.indexing.diagnostic.BrokenIndexingDiagnostics;
import com.intellij.util.indexing.impl.MapReduceIndexMappingException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@ApiStatus.Internal
final
class SingleIndexValueRemover {
  private final FileBasedIndexImpl myIndexImpl;
  final @NotNull ID<?, ?> indexId;
  private final int inputId;
  private final @Nullable String fileInfo;
  private final @NotNull FileIndexesValuesApplier.ApplicationMode applicationMode;
  long evaluatingValueRemoverTime;

  SingleIndexValueRemover(FileBasedIndexImpl indexImpl, @NotNull ID<?, ?> indexId,
                          @Nullable VirtualFile file,
                          @Nullable FileContent fileContent,
                          int inputId,
                          @NotNull FileIndexesValuesApplier.ApplicationMode applicationMode) {
    myIndexImpl = indexImpl;
    this.indexId = indexId;
    this.inputId = inputId;
    this.fileInfo = FileBasedIndexImpl.getFileInfoLogString(inputId, file, fileContent);
    this.applicationMode = applicationMode;
  }

  /**
   * @return false in case index update is not necessary or the update has failed
   */
  boolean remove() {
    if (!RebuildStatus.isOk(indexId) && !myIndexImpl.myIsUnitTestMode) {
      return false; // the index is scheduled for rebuild, no need to update
    }
    myIndexImpl.increaseLocalModCount();

    UpdatableIndex<?, ?, FileContent, ?> index = myIndexImpl.getIndex(indexId);

    FileBasedIndexImpl.markFileWritingIndexes(inputId);
    try {
      Supplier<Boolean> storageUpdate;
      long startTime = System.nanoTime();
      try {
        storageUpdate = index.mapInputAndPrepareUpdate(inputId, null);
      }
      catch (MapReduceIndexMappingException e) {
        BrokenIndexingDiagnostics.INSTANCE.getExceptionListener().onFileIndexMappingFailed(inputId, null, null, indexId, e);
        return false;
      }
      finally {
        this.evaluatingValueRemoverTime = System.nanoTime() - startTime;
      }

      if (myIndexImpl.runUpdateForPersistentData(storageUpdate)) {
        if (FileBasedIndexEx.doTraceStubUpdates(indexId) || FileBasedIndexEx.doTraceIndexUpdates()) {
          FileBasedIndexImpl.LOG.info("index " + indexId + " deletion finished for " + fileInfo);
        }
        ConcurrencyUtil.withLock(myIndexImpl.myReadLock, () -> {
          index.setUnindexedStateForFile(inputId);
        });
      }
      return true;
    }
    catch (RuntimeException exception) {
      myIndexImpl.requestIndexRebuildOnException(exception, indexId);
      return false;
    }
    finally {
      FileBasedIndexImpl.unmarkWritingIndexes();
    }
  }

  @Override
  public String toString() {
    return "SingleIndexValueRemover{" +
           "indexId=" + indexId +
           ", inputId=" + inputId +
           ", fileInfo='" + fileInfo + '\'' +
           ", applicationMode =" + applicationMode +
           '}';
  }
}
