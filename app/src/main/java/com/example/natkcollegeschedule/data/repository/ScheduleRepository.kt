package com.example.natkcollegeschedule.data.repository

import com.example.natkcollegeschedule.data.api.ScheduleApi
import com.example.natkcollegeschedule.data.dto.GroupDto
import com.example.natkcollegeschedule.data.dto.ScheduleByDateDto
class ScheduleRepository(private val api: ScheduleApi) {
    suspend fun loadSchedule(group: String, start: String, end: String): List<ScheduleByDateDto> {
        return api.getSchedule(
            groupName = group,
            start = "2026-01-26",
            end = "2026-02-07"
        )
    }
    suspend fun loadAllGroups(): List<GroupDto>
    {
        return api.getAllGroups()
    }
}