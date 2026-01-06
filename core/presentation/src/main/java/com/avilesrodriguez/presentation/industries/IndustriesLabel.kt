package com.avilesrodriguez.presentation.industries

import com.avilesrodriguez.domain.model.industries.IndustriesType
import com.avilesrodriguez.presentation.R

fun IndustriesType.label(): Int{
    return when(this){
    IndustriesType.INSURANCE -> R.string.insurance
    IndustriesType.REAL_ESTATE -> R.string.real_estate
    IndustriesType.OPTICS -> R.string.optics
    IndustriesType.FITNESS -> R.string.fitness
    IndustriesType.HEALTH -> R.string.health
    IndustriesType.BEAUTY -> R.string.beauty
    IndustriesType.TRAVEL -> R.string.travel
    IndustriesType.RETAIL -> R.string.retail
    IndustriesType.FINANCIAL -> R.string.financial
    IndustriesType.OTHER -> R.string.other
    }
}

fun IndustriesType.Companion.options(): List<Int>{
    return IndustriesType.entries.map { it.label() }
}

fun IndustriesType.Companion.getById(id: Int): IndustriesType {
    IndustriesType.entries.forEach { option ->
        if (option.label() == id) return option
    }
    return IndustriesType.OTHER
}