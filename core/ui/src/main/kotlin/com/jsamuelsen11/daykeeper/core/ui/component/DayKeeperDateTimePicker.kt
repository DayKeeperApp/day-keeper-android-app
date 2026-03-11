package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayKeeperDatePicker(
  onDateSelected: (epochMillis: Long) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  initialDateMillis: Long? = null,
  title: String = DayKeeperDateTimePickerDefaults.DATE_TITLE,
) {
  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

  DatePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = { datePickerState.selectedDateMillis?.let(onDateSelected) }) {
        Text(DayKeeperDateTimePickerDefaults.CONFIRM_LABEL)
      }
    },
    modifier = modifier,
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(DayKeeperDateTimePickerDefaults.DISMISS_LABEL) }
    },
  ) {
    DatePicker(
      state = datePickerState,
      title = {
        Text(text = title, modifier = Modifier, style = MaterialTheme.typography.labelLarge)
      },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayKeeperTimePicker(
  onTimeSelected: (hour: Int, minute: Int) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  initialHour: Int = DayKeeperDateTimePickerDefaults.DEFAULT_HOUR,
  initialMinute: Int = DayKeeperDateTimePickerDefaults.DEFAULT_MINUTE,
  title: String = DayKeeperDateTimePickerDefaults.TIME_TITLE,
) {
  val timePickerState =
    rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = { onTimeSelected(timePickerState.hour, timePickerState.minute) }) {
        Text(DayKeeperDateTimePickerDefaults.CONFIRM_LABEL)
      }
    },
    modifier = modifier,
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(DayKeeperDateTimePickerDefaults.DISMISS_LABEL) }
    },
    title = { Text(text = title, style = MaterialTheme.typography.headlineSmall) },
    text = { TimePicker(state = timePickerState) },
  )
}

private enum class DateTimePickerStep {
  DATE,
  TIME,
}

@Composable
fun DayKeeperDateTimePicker(
  onDateTimeSelected: (dateMillis: Long, hour: Int, minute: Int) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  initialDateMillis: Long? = null,
  initialHour: Int = DayKeeperDateTimePickerDefaults.DEFAULT_HOUR,
  initialMinute: Int = DayKeeperDateTimePickerDefaults.DEFAULT_MINUTE,
) {
  var step by remember { mutableStateOf(DateTimePickerStep.DATE) }
  var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

  when (step) {
    DateTimePickerStep.DATE -> {
      DayKeeperDatePicker(
        onDateSelected = { millis ->
          selectedDateMillis = millis
          step = DateTimePickerStep.TIME
        },
        onDismiss = onDismiss,
        modifier = modifier,
        initialDateMillis = initialDateMillis,
      )
    }
    DateTimePickerStep.TIME -> {
      DayKeeperTimePicker(
        onTimeSelected = { hour, minute ->
          selectedDateMillis?.let { dateMillis -> onDateTimeSelected(dateMillis, hour, minute) }
        },
        onDismiss = onDismiss,
        modifier = modifier,
        initialHour = initialHour,
        initialMinute = initialMinute,
      )
    }
  }
}

object DayKeeperDateTimePickerDefaults {
  const val DATE_TITLE = "Select date"
  const val TIME_TITLE = "Select time"
  const val DEFAULT_HOUR = 9
  const val DEFAULT_MINUTE = 0
  const val CONFIRM_LABEL = "OK"
  const val DISMISS_LABEL = "Cancel"
}

@Preview(showBackground = true)
@Composable
private fun DayKeeperDatePickerPreview() {
  DayKeeperTheme { DayKeeperDatePicker(onDateSelected = {}, onDismiss = {}) }
}

@Preview(showBackground = true)
@Composable
private fun DayKeeperTimePickerPreview() {
  DayKeeperTheme { DayKeeperTimePicker(onTimeSelected = { _, _ -> }, onDismiss = {}) }
}
