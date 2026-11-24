package com.rubp.whattoeat.server.feature.dish.data

import com.rubp.whattoeat.server.feature.stall.data.StallEntry
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "dish",
    uniqueConstraints = [UniqueConstraint(columnNames = ["stall_id", "name"])]
)
class DishEntry(
    @Id
    var id: Long,

    @Column(nullable = false, length = 80)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stall_id", nullable = false)
    var stall: StallEntry
)
