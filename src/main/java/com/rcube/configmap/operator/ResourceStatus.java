package com.rcube.configmap.operator;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class ResourceStatus {
    String errorMessage;
}
