/**
 * File: GenericResponse.kt
 * Authors: Martin Baláž
 * Description: This file have one data class, that defines a generic response object with a boolean flag indicating whether
 *              the operation was successful and a generic data object of type T which can hold any type of data returned by the operation.
 */

package com.example.util

/**
 * This data class defines a generic response object with a boolean flag indicating whether the operation was successful
 * and a generic data object of type T which can hold any type of data returned by the operation.
 */
data class GenericResponse<out T>(val isSuccess: Boolean, val data: T)
