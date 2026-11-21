package com.rubp.whattoeat.server.feature.location.data

import com.rubp.whattoeat.server.feature.location.domain.LocationKind
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "location")
class LocationEntry(
    @Id
    var id: Long,

    @Column(nullable = false, length = 100)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var kind: LocationKind
)
