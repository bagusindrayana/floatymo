package com.potadev.floatymo.data.remote

import com.google.gson.annotations.SerializedName

data class GiphySearchResponse(
    @SerializedName("data") val data: List<GiphyGif>,
    @SerializedName("pagination") val pagination: GiphyPagination,
    @SerializedName("meta") val meta: GiphyMeta
)

data class GiphyGif(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("images") val images: GiphyImages
)

data class GiphyImages(
    @SerializedName("fixed_width") val fixedWidth: GiphyImageData?,
    @SerializedName("original") val original: GiphyImageData?,
    @SerializedName("preview_gif") val previewGif: GiphyImageData?
)

data class GiphyImageData(
    @SerializedName("url") val url: String,
    @SerializedName("width") val width: String?,
    @SerializedName("height") val height: String?,
    @SerializedName("size") val size: String?
)

data class GiphyPagination(
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("count") val count: Int,
    @SerializedName("offset") val offset: Int
)

data class GiphyMeta(
    @SerializedName("status") val status: Int,
    @SerializedName("msg") val msg: String
)
