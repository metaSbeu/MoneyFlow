package com.example.moneyflow.utils

import com.example.moneyflow.R

object IconResolver {

    private val iconMap = mapOf(
        "ic_cash" to R.drawable.ic_cash,


        "ic_health" to R.drawable.ic_health,
        "ic_leisure" to R.drawable.ic_leisure,
        "ic_home" to R.drawable.ic_home,
        "ic_cafe" to R.drawable.ic_cafe,
        "ic_education" to R.drawable.ic_education,
        "ic_gift" to R.drawable.ic_gift,
        "ic_grocery" to R.drawable.ic_grocery,
        "ic_questionmark" to R.drawable.ic_questionmark,


        "ic_salary" to R.drawable.ic_salary,
        "ic_investments" to R.drawable.ic_investments,
        "ic_student" to R.drawable.ic_student,
        "ic_cashback" to R.drawable.ic_cashback,


        "logo_sber" to R.drawable.logo_sber,
        "logo_alfa" to R.drawable.logo_alfa,
        "logo_t_bank" to R.drawable.logo_t_bank,
        "logo_vtb" to R.drawable.logo_vtb,
        "logo_raiffaisen" to R.drawable.logo_raiffaisen,
        "logo_yandex" to R.drawable.logo_yandex,
        "logo_vk_pay" to R.drawable.logo_vk_pay,
        "logo_bitcoin" to R.drawable.logo_bitcoin,
        "logo_chelinvest" to R.drawable.logo_chelinvest,
        "logo_ozon" to R.drawable.logo_ozon,
        "logo_wb" to R.drawable.logo_wb,
        "logo_qiwi" to R.drawable.logo_qiwi,
        "logo_webmoney" to R.drawable.logo_webmoney,
        "logo_paypal" to R.drawable.logo_paypal,
        "logo_mts" to R.drawable.logo_mts,
        "logo_otkritie" to R.drawable.logo_otkritie,
        "logo_ubrir" to R.drawable.logo_ubrir,
        "logo_rosselhoz" to R.drawable.logo_rosselhoz,
        "logo_sovkom" to R.drawable.logo_sovkom,
        "logo_corona" to R.drawable.logo_corona,
        "logo_deutsche_bank" to R.drawable.logo_deutsche_bank,
        "logo_pb" to R.drawable.logo_pb,
        "logo_renessans" to R.drawable.logo_renessans,
        "logo_uralsib" to R.drawable.logo_uralsib,
        "ic_cash" to R.drawable.ic_cash,


        "ic_movie" to R.drawable.ic_movie,
        "ic_game" to R.drawable.ic_game,
        "ic_sport" to R.drawable.ic_sport,
        "ic_food" to R.drawable.ic_food,
        "ic_pet" to R.drawable.ic_pet,
        "ic_fruit" to R.drawable.ic_fruit,
        "ic_clothes" to R.drawable.ic_clothes,
        "ic_shoe" to R.drawable.ic_shoe,
        "ic_diamond" to R.drawable.ic_diamond,
        "ic_furniture" to R.drawable.ic_furniture,
        "ic_music" to R.drawable.ic_music,
        "ic_computer" to R.drawable.ic_computer,
        "ic_bicycle" to R.drawable.ic_bicycle,
        "ic_wifi" to R.drawable.ic_wifi,
        "ic_car" to R.drawable.ic_car,
        "ic_plane" to R.drawable.ic_plane,
        "ic_tooth" to R.drawable.ic_tooth,
        "ic_finance" to R.drawable.ic_finance,
        "ic_family" to R.drawable.ic_family,
        "ic_train_bus" to R.drawable.ic_train_bus,
        )

    fun resolve(iconName: String): Int {
        return iconMap[iconName] ?: R.drawable.ic_questionmark
    }
}