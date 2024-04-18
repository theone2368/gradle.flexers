/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.configurationcache.services

import org.gradle.api.IsolatedAction
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.invocation.Gradle
import org.gradle.configurationcache.isolation.IsolatedActionDeserializer
import org.gradle.configurationcache.isolation.IsolatedActionSerializer
import org.gradle.configurationcache.isolation.SerializedIsolatedActionGraph
import org.gradle.configurationcache.serialization.IsolateOwner
import org.gradle.configurationcache.serialization.serviceOf
import org.gradle.invocation.IsolatedProjectEvaluationListenerProvider


private
typealias IsolatedProjectAction = IsolatedAction<in Project>


private
typealias IsolatedProjectActionList = Collection<IsolatedProjectAction>


internal
class DefaultIsolatedProjectEvaluationListenerProvider : IsolatedProjectEvaluationListenerProvider {

    private
    val actions = mutableListOf<IsolatedProjectAction>()

    override fun beforeProject(action: IsolatedProjectAction) {
        actions.add(action)
    }

    override fun isolateFor(gradle: Gradle): ProjectEvaluationListener? = when {
        actions.isEmpty() -> null
        else -> {
            val isolate = isolate(actions, IsolateOwner.OwnerGradle(gradle))
            clear()
            IsolatedProjectEvaluationListener(gradle, isolate)
        }
    }

    override fun clear() {
        actions.clear()
    }

    private
    fun isolate(actions: Collection<IsolatedProjectAction>, owner: IsolateOwner) =
        IsolatedActionSerializer(owner, owner.serviceOf(), owner.serviceOf())
            .serialize(actions)
}


private
class IsolatedProjectEvaluationListener(
    private val gradle: Gradle,
    private val isolated: SerializedIsolatedActionGraph<IsolatedProjectActionList>
) : ProjectEvaluationListener {

    override fun beforeEvaluate(project: Project) {
        for (action in isolatedActions()) {
            action.execute(project)
        }
    }

    override fun afterEvaluate(project: Project, state: ProjectState) {
    }

    private
    fun isolatedActions() = IsolateOwner.OwnerGradle(gradle).let { owner ->
        IsolatedActionDeserializer(owner, owner.serviceOf(), owner.serviceOf())
            .deserialize(isolated)
    }
}
