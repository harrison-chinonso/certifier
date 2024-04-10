/*
 * *
 *  * Created by Kolawole Omirin
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 9/28/22, 1:30 PM
 *
 */

package org.dominion.city.dca.exceptions;

import lombok.*;

@Getter
@ToString
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExceptionResponse<T> {
    private T respBody;
    private String respCode;
    private String respDescription;

}
