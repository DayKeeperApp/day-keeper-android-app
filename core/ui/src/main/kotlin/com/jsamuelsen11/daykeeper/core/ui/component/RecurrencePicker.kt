package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceDay
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceEndCondition
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceFrequency
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class EndConditionType {
  NEVER,
  AFTER,
  ON_DATE,
}

@Stable
private class RecurrencePickerState(initialRule: RecurrenceRule?) {
  var frequency by mutableStateOf(initialRule?.frequency ?: RecurrenceFrequency.DAILY)
  var interval by
    mutableIntStateOf(initialRule?.interval ?: RecurrencePickerDefaults.DEFAULT_INTERVAL)
  var selectedDays by mutableStateOf(initialRule?.daysOfWeek ?: emptySet())
  var endConditionType by
    mutableStateOf(
      when (initialRule?.endCondition) {
        is RecurrenceEndCondition.AfterOccurrences -> EndConditionType.AFTER
        is RecurrenceEndCondition.UntilDate -> EndConditionType.ON_DATE
        else -> EndConditionType.NEVER
      }
    )
  var occurrenceCount by
    mutableIntStateOf(
      (initialRule?.endCondition as? RecurrenceEndCondition.AfterOccurrences)?.count
        ?: RecurrencePickerDefaults.DEFAULT_OCCURRENCES
    )
  var untilDateMillis by
    mutableStateOf((initialRule?.endCondition as? RecurrenceEndCondition.UntilDate)?.epochMillis)
  var showDatePicker by mutableStateOf(false)

  fun buildRule(): RecurrenceRule {
    val endCondition =
      when (endConditionType) {
        EndConditionType.NEVER -> RecurrenceEndCondition.Never
        EndConditionType.AFTER -> RecurrenceEndCondition.AfterOccurrences(count = occurrenceCount)
        EndConditionType.ON_DATE ->
          untilDateMillis?.let { RecurrenceEndCondition.UntilDate(epochMillis = it) }
            ?: RecurrenceEndCondition.Never
      }
    return RecurrenceRule(
      frequency = frequency,
      interval = interval,
      daysOfWeek = if (frequency == RecurrenceFrequency.WEEKLY) selectedDays else emptySet(),
      endCondition = endCondition,
    )
  }
}

@Composable
private fun rememberRecurrencePickerState(initialRule: RecurrenceRule?): RecurrencePickerState =
  remember {
    RecurrencePickerState(initialRule)
  }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrencePicker(
  onRecurrenceSelected: (RecurrenceRule?) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  initialRule: RecurrenceRule? = null,
) {
  val sheetState = rememberModalBottomSheetState()
  val state = rememberRecurrencePickerState(initialRule)

  if (state.showDatePicker) {
    DayKeeperDatePicker(
      onDateSelected = { millis ->
        state.untilDateMillis = millis
        state.showDatePicker = false
      },
      onDismiss = { state.showDatePicker = false },
      initialDateMillis = state.untilDateMillis,
    )
  }

  ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier, sheetState = sheetState) {
    RecurrencePickerContent(state = state, onRecurrenceSelected = onRecurrenceSelected)
  }
}

@Composable
private fun RecurrencePickerContent(
  state: RecurrencePickerState,
  onRecurrenceSelected: (RecurrenceRule?) -> Unit,
) {
  Column(modifier = Modifier.padding(horizontal = RecurrencePickerDefaults.SECTION_PADDING)) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(RecurrencePickerDefaults.SECTION_PADDING),
    ) {
      Icon(
        imageVector = DayKeeperIcons.Repeat,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(text = RecurrencePickerDefaults.TITLE, style = MaterialTheme.typography.titleLarge)
    }

    HorizontalDivider(
      modifier = Modifier.padding(vertical = RecurrencePickerDefaults.SECTION_PADDING)
    )

    FrequencySelector(frequency = state.frequency, onFrequencyChange = { state.frequency = it })

    IntervalSelector(
      interval = state.interval,
      frequency = state.frequency,
      onIntervalChange = { state.interval = it },
    )

    if (state.frequency == RecurrenceFrequency.WEEKLY) {
      DayOfWeekSelector(
        selectedDays = state.selectedDays,
        onDaysChange = { state.selectedDays = it },
      )
    }

    HorizontalDivider(
      modifier = Modifier.padding(vertical = RecurrencePickerDefaults.SECTION_PADDING)
    )

    EndConditionSelector(
      endConditionType = state.endConditionType,
      onEndConditionTypeChange = { state.endConditionType = it },
      occurrenceCount = state.occurrenceCount,
      onOccurrenceCountChange = { state.occurrenceCount = it },
      untilDateMillis = state.untilDateMillis,
      onPickDate = { state.showDatePicker = true },
    )

    Row(
      modifier =
        Modifier.fillMaxWidth().padding(vertical = RecurrencePickerDefaults.SECTION_PADDING),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      TextButton(onClick = { onRecurrenceSelected(null) }) {
        Text(RecurrencePickerDefaults.NO_REPEAT_LABEL)
      }
      TextButton(onClick = { onRecurrenceSelected(state.buildRule()) }) {
        Text(RecurrencePickerDefaults.SAVE_LABEL)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FrequencySelector(
  frequency: RecurrenceFrequency,
  onFrequencyChange: (RecurrenceFrequency) -> Unit,
) {
  SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
    RecurrenceFrequency.entries.forEachIndexed { index, freq ->
      SegmentedButton(
        selected = frequency == freq,
        onClick = { onFrequencyChange(freq) },
        shape =
          SegmentedButtonDefaults.itemShape(index = index, count = RecurrenceFrequency.entries.size),
      ) {
        Text(RecurrencePickerDefaults.frequencyLabels[freq] ?: freq.name)
      }
    }
  }
}

@Composable
private fun IntervalSelector(
  interval: Int,
  frequency: RecurrenceFrequency,
  onIntervalChange: (Int) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = RecurrencePickerDefaults.SECTION_PADDING),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(RecurrencePickerDefaults.SECTION_PADDING),
  ) {
    Text(RecurrencePickerDefaults.INTERVAL_LABEL, style = MaterialTheme.typography.bodyLarge)
    OutlinedTextField(
      value = interval.toString(),
      onValueChange = { value ->
        val parsed = value.filter { it.isDigit() }.toIntOrNull()
        if (parsed != null) {
          onIntervalChange(
            parsed.coerceIn(
              RecurrencePickerDefaults.MIN_INTERVAL,
              RecurrencePickerDefaults.MAX_INTERVAL,
            )
          )
        }
      },
      modifier = Modifier.width(RecurrencePickerDefaults.INTERVAL_FIELD_WIDTH),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      singleLine = true,
    )
    Text(
      RecurrencePickerDefaults.frequencyUnits[frequency] ?: "",
      style = MaterialTheme.typography.bodyLarge,
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayOfWeekSelector(
  selectedDays: Set<RecurrenceDay>,
  onDaysChange: (Set<RecurrenceDay>) -> Unit,
) {
  FlowRow(
    modifier = Modifier.fillMaxWidth().padding(vertical = RecurrencePickerDefaults.SECTION_PADDING),
    horizontalArrangement = Arrangement.spacedBy(RecurrencePickerDefaults.SECTION_PADDING / 2),
  ) {
    RecurrenceDay.entries.forEach { day ->
      FilterChip(
        selected = day in selectedDays,
        onClick = {
          onDaysChange(if (day in selectedDays) selectedDays - day else selectedDays + day)
        },
        label = { Text(RecurrencePickerDefaults.dayLabels[day] ?: day.name) },
      )
    }
  }
}

@Composable
private fun EndConditionSelector(
  endConditionType: EndConditionType,
  onEndConditionTypeChange: (EndConditionType) -> Unit,
  occurrenceCount: Int,
  onOccurrenceCountChange: (Int) -> Unit,
  untilDateMillis: Long?,
  onPickDate: () -> Unit,
) {
  Text(RecurrencePickerDefaults.ENDS_LABEL, style = MaterialTheme.typography.titleMedium)

  EndConditionRow(
    label = RecurrencePickerDefaults.NEVER_LABEL,
    isSelected = endConditionType == EndConditionType.NEVER,
    onClick = { onEndConditionTypeChange(EndConditionType.NEVER) },
  )

  EndConditionRow(
    label = RecurrencePickerDefaults.AFTER_LABEL,
    isSelected = endConditionType == EndConditionType.AFTER,
    onClick = { onEndConditionTypeChange(EndConditionType.AFTER) },
  ) {
    OutlinedTextField(
      value = occurrenceCount.toString(),
      onValueChange = { value ->
        value.filter { it.isDigit() }.toIntOrNull()?.let(onOccurrenceCountChange)
      },
      modifier = Modifier.width(RecurrencePickerDefaults.OCCURRENCES_FIELD_WIDTH),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      singleLine = true,
    )
    Text(RecurrencePickerDefaults.OCCURRENCES_LABEL, style = MaterialTheme.typography.bodyMedium)
  }

  EndConditionRow(
    label = RecurrencePickerDefaults.ON_DATE_LABEL,
    isSelected = endConditionType == EndConditionType.ON_DATE,
    onClick = { onEndConditionTypeChange(EndConditionType.ON_DATE) },
  ) {
    TextButton(onClick = onPickDate) {
      Text(
        untilDateMillis?.let { RecurrencePickerDefaults.formatDate(it) } ?: "Select date",
        style = MaterialTheme.typography.bodyMedium,
      )
    }
  }
}

@Composable
private fun EndConditionRow(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  content: @Composable (() -> Unit)? = null,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(RecurrencePickerDefaults.SECTION_PADDING / 2),
  ) {
    RadioButton(selected = isSelected, onClick = onClick)
    Text(label, style = MaterialTheme.typography.bodyLarge)
    content?.invoke()
  }
}

object RecurrencePickerDefaults {
  const val TITLE = "Repeat"
  const val INTERVAL_LABEL = "Every"
  const val ENDS_LABEL = "Ends"
  const val NEVER_LABEL = "Never"
  const val AFTER_LABEL = "After"
  const val ON_DATE_LABEL = "On date"
  const val OCCURRENCES_LABEL = "occurrences"
  const val SAVE_LABEL = "Save"
  const val NO_REPEAT_LABEL = "Don't repeat"
  const val DEFAULT_INTERVAL = 1
  const val MIN_INTERVAL = 1
  const val MAX_INTERVAL = 99
  const val DEFAULT_OCCURRENCES = 10
  val SECTION_PADDING = 16.dp
  val INTERVAL_FIELD_WIDTH = 64.dp
  val OCCURRENCES_FIELD_WIDTH = 72.dp

  internal val frequencyLabels =
    mapOf(
      RecurrenceFrequency.DAILY to "Daily",
      RecurrenceFrequency.WEEKLY to "Weekly",
      RecurrenceFrequency.MONTHLY to "Monthly",
      RecurrenceFrequency.YEARLY to "Yearly",
    )

  internal val frequencyUnits =
    mapOf(
      RecurrenceFrequency.DAILY to "days",
      RecurrenceFrequency.WEEKLY to "weeks",
      RecurrenceFrequency.MONTHLY to "months",
      RecurrenceFrequency.YEARLY to "years",
    )

  internal val dayLabels =
    mapOf(
      RecurrenceDay.MONDAY to "Mon",
      RecurrenceDay.TUESDAY to "Tue",
      RecurrenceDay.WEDNESDAY to "Wed",
      RecurrenceDay.THURSDAY to "Thu",
      RecurrenceDay.FRIDAY to "Fri",
      RecurrenceDay.SATURDAY to "Sat",
      RecurrenceDay.SUNDAY to "Sun",
    )

  internal fun formatDate(epochMillis: Long): String {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return dateFormat.format(Date(epochMillis))
  }
}

@Preview(showBackground = true)
@Composable
private fun RecurrencePickerPreview() {
  DayKeeperTheme { RecurrencePicker(onRecurrenceSelected = {}, onDismiss = {}) }
}

@Preview(showBackground = true)
@Composable
private fun RecurrencePickerWithInitialRulePreview() {
  DayKeeperTheme {
    RecurrencePicker(
      onRecurrenceSelected = {},
      onDismiss = {},
      initialRule =
        RecurrenceRule(
          frequency = RecurrenceFrequency.WEEKLY,
          interval = 2,
          daysOfWeek = setOf(RecurrenceDay.MONDAY, RecurrenceDay.WEDNESDAY, RecurrenceDay.FRIDAY),
        ),
    )
  }
}
