package com.kageguard.backend.simulator;

// What the control API returns: AUTO or MANUAL, and the held level (null in AUTO)
public record ControlState(String mode, Integer manualLevel) {
}