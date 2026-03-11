package com.jsamuelsen11.daykeeper.core.model

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecurrenceRuleTest {

  @Nested
  inner class ToRruleString {

    @Test
    fun `daily with default interval`() {
      val rule = RecurrenceRule(frequency = RecurrenceFrequency.DAILY)
      rule.toRruleString() shouldBe "FREQ=DAILY"
    }

    @Test
    fun `daily with custom interval`() {
      val rule = RecurrenceRule(frequency = RecurrenceFrequency.DAILY, interval = 3)
      rule.toRruleString() shouldBe "FREQ=DAILY;INTERVAL=3"
    }

    @Test
    fun `weekly with days of week`() {
      val rule =
        RecurrenceRule(
          frequency = RecurrenceFrequency.WEEKLY,
          daysOfWeek = setOf(RecurrenceDay.MONDAY, RecurrenceDay.WEDNESDAY, RecurrenceDay.FRIDAY),
        )
      val rrule = rule.toRruleString()
      rrule shouldBe "FREQ=WEEKLY;BYDAY=MO,WE,FR"
    }

    @Test
    fun `monthly with default interval`() {
      val rule = RecurrenceRule(frequency = RecurrenceFrequency.MONTHLY)
      rule.toRruleString() shouldBe "FREQ=MONTHLY"
    }

    @Test
    fun `yearly with default interval`() {
      val rule = RecurrenceRule(frequency = RecurrenceFrequency.YEARLY)
      rule.toRruleString() shouldBe "FREQ=YEARLY"
    }

    @Test
    fun `with count end condition`() {
      val rule =
        RecurrenceRule(
          frequency = RecurrenceFrequency.DAILY,
          endCondition = RecurrenceEndCondition.AfterOccurrences(count = 10),
        )
      rule.toRruleString() shouldBe "FREQ=DAILY;COUNT=10"
    }

    @Test
    fun `with until end condition`() {
      val epochMillis = 1_709_251_200_000L
      val rule =
        RecurrenceRule(
          frequency = RecurrenceFrequency.WEEKLY,
          endCondition = RecurrenceEndCondition.UntilDate(epochMillis = epochMillis),
        )
      rule.toRruleString() shouldBe "FREQ=WEEKLY;UNTIL=$epochMillis"
    }

    @Test
    fun `complex rule with all parts`() {
      val rule =
        RecurrenceRule(
          frequency = RecurrenceFrequency.WEEKLY,
          interval = 2,
          daysOfWeek = setOf(RecurrenceDay.TUESDAY, RecurrenceDay.THURSDAY),
          endCondition = RecurrenceEndCondition.AfterOccurrences(count = 5),
        )
      rule.toRruleString() shouldBe "FREQ=WEEKLY;INTERVAL=2;BYDAY=TU,TH;COUNT=5"
    }
  }

  @Nested
  inner class FromRruleString {

    @Test
    fun `parses simple daily`() {
      val rule = RecurrenceRule.fromRruleString("FREQ=DAILY")
      rule.frequency shouldBe RecurrenceFrequency.DAILY
      rule.interval shouldBe 1
      rule.daysOfWeek.shouldBeEmpty()
      rule.endCondition shouldBe RecurrenceEndCondition.Never
    }

    @Test
    fun `parses daily with interval`() {
      val rule = RecurrenceRule.fromRruleString("FREQ=DAILY;INTERVAL=3")
      rule.frequency shouldBe RecurrenceFrequency.DAILY
      rule.interval shouldBe 3
    }

    @Test
    fun `parses weekly with days`() {
      val rule = RecurrenceRule.fromRruleString("FREQ=WEEKLY;BYDAY=MO,WE,FR")
      rule.frequency shouldBe RecurrenceFrequency.WEEKLY
      rule.daysOfWeek shouldBe
        setOf(RecurrenceDay.MONDAY, RecurrenceDay.WEDNESDAY, RecurrenceDay.FRIDAY)
    }

    @Test
    fun `parses count end condition`() {
      val rule = RecurrenceRule.fromRruleString("FREQ=DAILY;COUNT=10")
      rule.endCondition shouldBe RecurrenceEndCondition.AfterOccurrences(count = 10)
    }

    @Test
    fun `parses until end condition`() {
      val rule = RecurrenceRule.fromRruleString("FREQ=WEEKLY;UNTIL=1709251200000")
      rule.endCondition shouldBe RecurrenceEndCondition.UntilDate(epochMillis = 1_709_251_200_000L)
    }
  }

  @Nested
  inner class RoundTrip {

    @Test
    fun `round trip preserves all fields`() {
      val original =
        RecurrenceRule(
          frequency = RecurrenceFrequency.WEEKLY,
          interval = 2,
          daysOfWeek = setOf(RecurrenceDay.MONDAY, RecurrenceDay.FRIDAY),
          endCondition = RecurrenceEndCondition.AfterOccurrences(count = 12),
        )
      val parsed = RecurrenceRule.fromRruleString(original.toRruleString())
      parsed shouldBe original
    }

    @Test
    fun `round trip with never end condition`() {
      val original = RecurrenceRule(frequency = RecurrenceFrequency.MONTHLY)
      val parsed = RecurrenceRule.fromRruleString(original.toRruleString())
      parsed shouldBe original
    }
  }

  @Nested
  inner class RecurrenceDayEnum {

    @Test
    fun `all days have unique abbreviations`() {
      val abbrevs = RecurrenceDay.entries.map { it.rruleAbbrev }
      abbrevs.distinct().size shouldBe RecurrenceDay.entries.size
    }

    @Test
    fun `abbreviations are two characters`() {
      RecurrenceDay.entries.forEach { day -> day.rruleAbbrev.length shouldBe 2 }
    }
  }

  @Nested
  inner class RecurrenceFrequencyEnum {

    @Test
    fun `has four frequencies`() {
      RecurrenceFrequency.entries.size shouldBe 4
    }

    @Test
    fun `all frequencies have unique rrule values`() {
      val values = RecurrenceFrequency.entries.map { it.rruleValue }
      values.distinct().size shouldBe RecurrenceFrequency.entries.size
    }
  }
}
