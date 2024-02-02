package com.example.courseworkapplication

/* This is the Reviews data class used to store information about reviews such as who made the review and
* which restaurant the review is referring to. */
data class Reviews(val user : String? = null, val restaurant : String? = null, val rating : Float? = null,
                   val reviewText : String? = null, val location : String? = null, val imgUrl : String? = null,
                   val reviewTitle : String? = null, val identifier : String? = null, val likeCount : Int? = null)