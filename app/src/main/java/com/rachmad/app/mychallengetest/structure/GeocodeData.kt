package com.rachmad.app.mychallengetest.structure

data class GeocodeData(
    val plus_code: plusCode?,
    val results: ArrayList<Result?>?,
    val status: String?,
)

data class Result(
    val address_components: ArrayList<addressComponent?>?,
    val formatted_address: String?,
    val geometry: Geometry?,
    val place_id: String?,
    val plus_code: plusCode?,
    val types: ArrayList<String?>?,
    val name: String?
)

data class plusCode(
    val compound_code: String?,
    val global_code: String?
)

data class addressComponent(
    val long_name: String?,
    val short_name: String?,
    val types: ArrayList<String?>?,
)

data class Geometry(
    val location: Location?,
    val location_type: String?,
    val viewport: Viewport?
)

data class Viewport(
    val northeast: Location?,
    val southwest: Location?
)

data class Location(
    val lat: Double?,
    val lng: Double?
)