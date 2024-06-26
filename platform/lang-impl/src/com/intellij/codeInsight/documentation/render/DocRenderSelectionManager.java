// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.documentation.render;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class DocRenderSelectionManager implements CaretListener, SelectionListener, Disposable {
  private static final Key<DocRenderSelectionManager> OUR_KEY = Key.create("DocRenderSelectionManager");

  private final Editor myEditor;

  private DocRenderer.EditorInlineHtmlPane myPaneWithSelection;
  private boolean mySkipSelectionEvents;

  DocRenderSelectionManager(Editor editor) {
    myEditor = editor;
    editor.getCaretModel().addCaretListener(this, this);
    editor.getSelectionModel().addSelectionListener(this, this);
    editor.putUserData(OUR_KEY, this);
  }

  @Override
  public void dispose() {
    myEditor.putUserData(OUR_KEY, null);
  }

  void setPaneWithSelection(DocRenderer.EditorInlineHtmlPane pane) {
    if (pane != myPaneWithSelection) {
      if (myPaneWithSelection != null) {
        myPaneWithSelection.removeSelection();
      }
      myPaneWithSelection = pane;
      if (myPaneWithSelection != null) {
        mySkipSelectionEvents = true;
        try {
          myEditor.getSelectionModel().removeSelection(true);
        }
        finally {
          mySkipSelectionEvents = false;
        }
      }
    }
  }

  @Override
  public void caretPositionChanged(@NotNull CaretEvent event) {
    setPaneWithSelection(null);
  }

  @Override
  public void caretAdded(@NotNull CaretEvent event) {
    setPaneWithSelection(null);
  }

  @Override
  public void caretRemoved(@NotNull CaretEvent event) {
    setPaneWithSelection(null);
  }

  @Override
  public void selectionChanged(@NotNull SelectionEvent e) {
    if (!mySkipSelectionEvents) setPaneWithSelection(null);
  }

  public static @Nullable DocRenderer.EditorInlineHtmlPane getPaneWithSelection(@NotNull Editor editor) {
    DocRenderSelectionManager selectionManager = editor.getUserData(OUR_KEY);
    return selectionManager == null ? null : selectionManager.myPaneWithSelection;
  }
}
