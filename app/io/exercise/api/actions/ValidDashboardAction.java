package io.exercise.api.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import io.exercise.api.models.Dashboard;
import io.exercise.api.models.validators.HibernateValidator;
import io.exercise.api.models.validators.ValidDashboard;

import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ValidDashboardAction extends Action<ValidDashboard> {

    @Override
    public CompletionStage<Result> call(Http.Request request) {
        try {
            JsonNode node = request.body().asJson();
            if (!node.isObject()) {
                return CompletableFuture.completedFuture(badRequest("Expecting object, not array!"));
            }
            Dashboard dashboard = Json.fromJson(node, Dashboard.class);

            String errors = HibernateValidator.validate(dashboard);
            if (!Strings.isNullOrEmpty(errors)) {
                return CompletableFuture.supplyAsync(() -> badRequest(errors));
            }
        } catch (Exception ex) {
            return CompletableFuture.completedFuture(badRequest("Bad input"));
        }

        return delegate.call(request);
    }
}
