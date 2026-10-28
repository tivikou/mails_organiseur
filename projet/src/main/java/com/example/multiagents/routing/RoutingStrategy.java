package com.example.multiagents.routing;

import com.example.multiagents.domain.agent.Agent;
import com.example.multiagents.domain.email.Email;
import java.util.List;

public interface RoutingStrategy {
    RoutingDecision select(Email email, List<Agent> availableAgents);
}
