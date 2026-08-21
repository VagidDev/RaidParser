package com.unifun.raidparser.controllers.error;

import java.time.Instant;

/**
 * Единый формат ошибки: клиенту достаточно смотреть на error и message,
 * не разбирая разные тела ответов от разных слоёв.
 */
public record ApiError(String error, String message, Instant timestamp) {
}
