package com.unifun.raidparser.controllers.dto;

import com.unifun.raidparser.core.component.Severity;

/**
 * Состояние одного компонента сервера.
 *
 * @param status   исходное название статуса от вендора, например "OK(Predictive Failure)"
 * @param severity обобщённая классификация для клиента
 * @param priority внутренний приоритет, меньше — серьёзнее
 * @param details  строки вывода, из которых сделан вывод о статусе
 */
public record ComponentStatusResponse(
        String status,
        Severity severity,
        int priority,
        String details
) {
}
