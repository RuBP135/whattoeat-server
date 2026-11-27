package com.rubp.whattoeat.server.feature.location.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "location",
    uniqueConstraints = [UniqueConstraint(columnNames = ["building_id", "name"])]
)
class LocationEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id", nullable = false)
    var building: BuildingEntry,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false)
    var ordinal: Int
)
