package com.github.thomasdarimont.keycloak.auth.requiregroup;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

/**
 * Simple {@link Authenticator} that checks if a user is a member of a given {@link GroupModel Group}.
 */
public class RequireGroupAuthenticator implements Authenticator {

    private static final Logger LOG = Logger.getLogger(RequireGroupAuthenticator.class);

    private AuthenticationFlowContext context;

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        this.context = context;

        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();

        String groupPath = configModel.getConfig().get(RequireGroupAuthenticatorFactory.GROUP);
        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();

        if (!isMemberOfGroup(realm, user, groupPath)) {
            LOG.debugf("Access denied because of missing group membership. realm=%s username=%s groupPath=%s", realm.getName(), user.getUsername(), groupPath);
            context.cancelLogin();
            return;
        }

        context.success();
    }

    private boolean isMemberOfGroup(RealmModel realm, UserModel user, String groupPath) {
        if (groupPath == null) {
            return false;
        }

        // Assuming you have a KeycloakSession available, obtain it from the AuthenticationFlowContext
        KeycloakSession keycloakSession = context.getSession();
        
        // Corrected method signature for Keycloak version 23.0.6
        GroupModel group = KeycloakModelUtils.findGroupByPath(keycloakSession, realm, groupPath);

        return group != null && user.isMemberOf(group);
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // NOOP
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }
}
