package com.rubp.whattoeat.server.feature.stall.data

import com.rubp.whattoeat.server.feature.location.data.LocationEntry
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Index

@Entity
@Table(
    name = "stall",
    indexes = [Index(columnList = "location_id, slot, id")],
)
class StallEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var slot: Int,

    @Column(nullable = false, length = 100)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    var location: LocationEntry,

    @Column(nullable = false)
    var active: Boolean
) {
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "stall_category_link",
        joinColumns = [JoinColumn(name = "stall_id", nullable = false)],
        inverseJoinColumns = [JoinColumn(name = "category_id", nullable = false)],
        indexes = [
            Index(columnList = "category_id")
        ],
    )
    var categories: MutableSet<StallCategoryEntry> = mutableSetOf()
}
