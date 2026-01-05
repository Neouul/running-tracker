package com.neouul.runningtracker.domain.model

/**
 * 외부 라이브러리(Google Maps)에 의존하지 않는 도메인 전용 위치 모델
 */
data class LocationPoint(
    val latitude: Double,
    val longitude: Double
)

/**
 * 일시 정지 시점마다 끊어지는 경로 묶음
 */
typealias TrackingPath = List<List<LocationPoint>>
