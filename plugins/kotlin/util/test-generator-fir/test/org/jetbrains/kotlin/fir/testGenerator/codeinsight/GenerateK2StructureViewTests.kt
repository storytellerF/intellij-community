// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.fir.testGenerator.codeinsight

import org.jetbrains.kotlin.idea.k2.structureView.AbstractKotlinFirFileStructureTest
import org.jetbrains.kotlin.testGenerator.model.*
import org.jetbrains.kotlin.testGenerator.model.GroupCategory.*


internal fun MutableTWorkspace.generateK2StructureViewTests() {
    testGroup("code-insight/kotlin.code-insight.k2", category = CODE_INSIGHT) {
         testClass<AbstractKotlinFirFileStructureTest> {
            model("../../../idea/tests/testData/structureView/fileStructure", pattern = Patterns.KT_OR_KTS_WITHOUT_DOTS)
        }
    }
}