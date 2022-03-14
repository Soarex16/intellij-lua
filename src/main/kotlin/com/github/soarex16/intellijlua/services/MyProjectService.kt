package com.github.soarex16.intellijlua.services

import com.intellij.openapi.project.Project
import com.github.soarex16.intellijlua.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
