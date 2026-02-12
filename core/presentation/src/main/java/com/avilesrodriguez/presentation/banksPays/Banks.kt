package com.avilesrodriguez.presentation.banksPays

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.avilesrodriguez.presentation.R

enum class BanksEcuador(
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int,
    val packageName: String
){
    BANK_OF_PICHINCHA(
        R.string.bank_of_pichincha,
        R.drawable.pichincha,
        "com.yellowpepper.pichincha"
    ),
    BANK_OF_GUAYAQUIL(
        R.string.bank_of_guayaquil,
        R.drawable.guayaquil,
        "com.bancodeguayaquil"
    ),
    BANK_OF_PACIFIC(
        R.string.bank_of_pacific,
        R.drawable.bdp,
        "com.pacifico.movilmatico"
    ),
    PRODUBANCO(
        R.string.produbanco,
        R.drawable.produbanco,
        "com.produbanco"
    ),
    BANK_BOLIVARIANO(
        R.string.bank_bolivariano,
        R.drawable.bolivariano,
        "com.bancobolivariano"
    ),
    BANK_INTERNATIONAL(
        R.string.bank_international,
        R.drawable.internacional,
        "com.baninter"
    ),
    BANK_AUSTRO(
        R.string.bank_austro,
        R.drawable.austro,
        "fin.austro.austrodigital"
    );
    companion object
}

fun BanksEcuador.Companion.options(): List<Pair<Int, Int>>{
    return listOf(R.string.choose_your_bank to R.drawable.bank) + BanksEcuador.entries.map { it.label to it.icon }
}

fun BanksEcuador.Companion.getById(id: Int): BanksEcuador? {
    if(id == R.string.choose_your_bank) return null
    return BanksEcuador.entries.find { option ->
        option.label == id
    }?: BanksEcuador.BANK_OF_PICHINCHA
}