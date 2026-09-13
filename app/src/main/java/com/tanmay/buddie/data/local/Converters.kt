package com.tanmay.buddie.data.local

import androidx.room.TypeConverter
import com.tanmay.buddie.data.local.entity.SessionType
import com.tanmay.buddie.data.local.entity.SubmissionStatus
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.format(DateTimeFormatter.ISO_LOCAL_TIME)

    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? = timeString?.let { LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME) }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? = dateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? = dateTimeString?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }

    @TypeConverter
    fun fromDayOfWeek(day: DayOfWeek?): String? = day?.name

    @TypeConverter
    fun toDayOfWeek(dayString: String?): DayOfWeek? = dayString?.let { DayOfWeek.valueOf(it) }

    @TypeConverter
    fun fromSessionType(type: SessionType?): String? = type?.name

    @TypeConverter
    fun toSessionType(typeString: String?): SessionType? = typeString?.let { SessionType.valueOf(it) }

    @TypeConverter
    fun fromSubmissionStatus(status: SubmissionStatus?): String? = status?.name

    @TypeConverter
    fun toSubmissionStatus(statusString: String?): SubmissionStatus? = statusString?.let { SubmissionStatus.valueOf(it) }
}
