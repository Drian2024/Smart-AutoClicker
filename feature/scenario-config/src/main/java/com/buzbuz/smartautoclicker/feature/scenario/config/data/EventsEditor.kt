/*
 * Copyright (C) 2023 Kevin Buzeau
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.buzbuz.smartautoclicker.feature.scenario.config.data

import com.buzbuz.smartautoclicker.core.domain.model.AND
import com.buzbuz.smartautoclicker.core.domain.model.OR
import com.buzbuz.smartautoclicker.core.domain.model.action.Action
import com.buzbuz.smartautoclicker.core.domain.model.condition.Condition
import com.buzbuz.smartautoclicker.core.domain.model.event.Event
import com.buzbuz.smartautoclicker.core.domain.model.scenario.Scenario
import com.buzbuz.smartautoclicker.feature.scenario.config.data.base.ListEditor

import kotlinx.coroutines.flow.StateFlow

class EventsEditor(
    private val onDeleteEvent: (Event) -> Unit,
    parentItem: StateFlow<Scenario?>,
): ListEditor<Event, Scenario>(parentItem = parentItem) {

    override fun areItemsTheSame(a: Event, b: Event): Boolean = a.id == b.id
    override fun isItemComplete(item: Event, parent: Scenario?): Boolean = item.isComplete()

    val conditionsEditor = object : ListEditor<Condition, Event>(::onEditedEventConditionsUpdated, false, parentItem = editedItem) {
        override fun areItemsTheSame(a: Condition, b: Condition): Boolean = a.id == b.id
        override fun isItemComplete(item: Condition, parent: Event?): Boolean = item.isComplete()
    }

    val actionsEditor = ActionsEditor(::onEditedEventActionsUpdated, parentItem = editedItem)

    override fun startItemEdition(item: Event) {
        super.startItemEdition(item)
        conditionsEditor.startEdition(item.conditions)
        actionsEditor.startEdition(item.actions)
    }

    override fun deleteEditedItem() {
        val editedItem = editedItem.value ?: return
        onDeleteEvent(editedItem)
        super.deleteEditedItem()
    }

    override fun stopItemEdition() {
        actionsEditor.stopEdition()
        conditionsEditor.stopEdition()
        super.stopItemEdition()
    }

    fun deleteAllActionsReferencing(event: Event) {
        val events = editedList.value ?: return

        val newEvents = events.mapNotNull { scenarioEvent ->
            if (scenarioEvent.id == event.id) return@mapNotNull null // Skip same item

            val newActions = scenarioEvent.actions.toMutableList()
            scenarioEvent.actions.forEach { action ->
                if (action is Action.ToggleEvent && action.toggleEventId == event.id) newActions.remove(action)
            }

            scenarioEvent.copy(actions = newActions)
        }

        updateList(newEvents)
    }

private fun onEditedEventConditionsUpdated(conditions: List<Condition>) {
    val editedEvent = editedItem.value ?: return

    actionsEditor.editedList.value?.let { actions ->
        val newActions = actions.toMutableList()

        for (condition in conditions) {
            // Skip conditions that already have a corresponding clicking action
            if (editedEvent.conditionOperator == OR && newActions.any { it is Action.Click && it.clickOnConditionId == condition.id }) {
                continue
            }

            // Perform the clicking logic based on conditions
            val conditionMet = checkIfConditionMet(condition.id, conditions)

            // Add clicking action for the condition if it's met
            if (conditionMet) {
                newActions.add(Action.Click(/* Your click parameters */, clickOnConditionId = condition.id))
            }
        }

        actionsEditor.updateList(newActions)
    }

    editedItem.value?.let { event ->
        updateEditedItem(event.copy(conditions = conditions))
    }
}

private fun checkIfConditionMet(conditionId: String?, conditions: List<Condition>): Boolean {
    // Add your logic here to check if the condition is met
    // For example, check if the conditionId exists in the list of conditions
    return conditionId?.let { id ->
        conditions.any { it.id == id }
    } ?: false
}


private fun checkIfConditionMet(conditionId: String?, conditions: List<Condition>): Boolean {
    // Add your logic here to check if the condition is met
    // For example, check if the conditionId exists in the list of conditions
    return conditionId?.let { id ->
        conditions.any { it.id == id }
    } ?: false
}


private fun checkIfConditionMet(conditionId: String?, conditions: List<Condition>): Boolean {
    // Add your logic here to check if the condition is met
    // For example, check if the conditionId exists in the list of conditions
    return conditionId?.let { id ->
        conditions.any { it.id == id }
    } ?: false
}



    private fun onEditedEventActionsUpdated(actions: List<Action>) {
        editedItem.value?.let { event ->
            updateEditedItem(event.copy(actions = actions))
        }
    }
}
