package ru.netology.nmedia.data

open class ApiException(message: String? = null) : Exception(message)

// Специфичные исключения
class AuthRequiredException : ApiException("Требуется авторизация")
class NotFoundException : ApiException("Данные не найдены")
class ServerErrorException : ApiException("Ошибка сервера")
class NetworkException : ApiException("Нет соединения с интернетом")

// Дополнительные исключения по необходимости
class BadRequestException : ApiException("Неверный запрос")
class ForbiddenException : ApiException("Доступ запрещен")