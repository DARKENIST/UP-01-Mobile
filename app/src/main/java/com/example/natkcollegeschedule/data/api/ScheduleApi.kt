package com.example.natkcollegeschedule.data.api

import com.example.natkcollegeschedule.data.dto.GroupDto
import com.example.natkcollegeschedule.data.dto.ScheduleByDateDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ScheduleApi {

    @GET("api/group/all")
    suspend fun getAllGroups(

    ): List<GroupDto>
    @GET("api/schedule/group/{groupName}")
    suspend fun getSchedule(
        @Path("groupName") groupName: String,
        @Query("start") start: String,
        @Query("end") end: String
    ): List<ScheduleByDateDto>
}
