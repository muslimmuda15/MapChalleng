package com.rachmad.app.mychallengetest.structure

data class PredictionData(
    val predictions: ArrayList<Prediction?>?,
    val status: String?
)

data class Prediction(
    val description: String?,
    val matched_substrings: ArrayList<Substring?>?,
    val place_id: String?,
    val reference: String?,
    val structured_formatting: StructureFormatting?,
    val terms: ArrayList<Terms?>?,
    val types: ArrayList<String?>?
)

data class Terms(
    val offset: Int?,
    val value: String?
)

data class StructureFormatting(
    val main_text: String?,
    val main_text_matched_substrings: ArrayList<Substring?>?,
    val secondary_text: String?
)

data class Substring(
    val length: Int?,
    val offset: Int?
)