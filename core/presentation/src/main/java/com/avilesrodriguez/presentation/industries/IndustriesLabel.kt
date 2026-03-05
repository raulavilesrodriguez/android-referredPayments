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

fun IndustriesType.icons(): Int{
    return when(this){
        IndustriesType.INSURANCE -> R.drawable.insurance
        IndustriesType.REAL_ESTATE -> R.drawable.real_estate
        IndustriesType.OPTICS -> R.drawable.optics
        IndustriesType.FITNESS -> R.drawable.fitness
        IndustriesType.HEALTH -> R.drawable.health
        IndustriesType.BEAUTY -> R.drawable.beauty
        IndustriesType.TRAVEL -> R.drawable.travel
        IndustriesType.RETAIL -> R.drawable.retail
        IndustriesType.FINANCIAL -> R.drawable.financial
        IndustriesType.OTHER -> R.drawable.other
    }
}

fun IndustriesType.Companion.options(search: Boolean): List<Int>{
    if(search){
        return listOf(R.string.all_industries) + IndustriesType.entries.map { it.label() }
    }
    return IndustriesType.entries.map { it.label() }
}

fun IndustriesType.Companion.getById(id: Int): IndustriesType? {
    if (id == R.string.all_industries) return null
    return IndustriesType.entries.find { option ->
        option.label() == id
    } ?: IndustriesType.OTHER
}