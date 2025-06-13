package com.jlss.smartDairy.viewmodel



data class EntryRowState(
    var serialNo: Int,
    var name: String = "",
    var fatRate: String = "",    // keep as text for TextField binding
    var milkQty: String = "",
    val amount: Double = 0.0     // computed or manual override
)
