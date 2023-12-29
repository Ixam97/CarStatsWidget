package de.ixam97.carstatswidget.repository.responses

data class PolestarGetCars(
    val `data`: Data
) {
    data class Data(
        val getConsumerCarsV2: List<GetConsumerCarsV2>
    ) {
        data class GetConsumerCarsV2(
            val content: Content,
            val vin: String
        ) {
            data class Content(
                val images: Images,
                val model: Model
            ) {
                data class Images(
                    val studio: Studio
                ) {
                    data class Studio(
                        val angles: List<String>,
                        val url: String
                    )
                }

                data class Model(
                    val code: String,
                    val name: String
                )
            }
        }
    }
}