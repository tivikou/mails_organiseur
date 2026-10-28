package com.example.multiagents.routing;

import com.example.multiagents.domain.agent.Agent;

public record RoutingDecision(Agent agent, double confidence, String explanation) {
}
