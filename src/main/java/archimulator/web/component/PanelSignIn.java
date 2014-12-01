/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.web.component;

import de.agilecoders.wicket.markup.html.bootstrap.common.NotificationPanel;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

/**
 * Sign in panel.
 *
 * @author Min Cai
 */
public class PanelSignIn extends Panel {
    private boolean rememberMe = true;
    private String email;
    private String password;

    /**
     * Create a sign in panel.
     *
     * @param id the markup ID of the panel that is to be created
     */
    public PanelSignIn(String id) {
        super(id);

        add(new NotificationPanel("feedback"));
        add(new SignInForm("signIn"));
    }

    @Override
    protected void onBeforeRender() {
        if (!isSignedIn()) {
            IAuthenticationStrategy authenticationStrategy = getApplication().getSecuritySettings()
                    .getAuthenticationStrategy();
            String[] data = authenticationStrategy.load();

            if ((data != null) && (data.length > 1)) {
                if (signIn(data[0], data[1])) {
                    email = data[0];
                    password = data[1];

                    continueToOriginalDestination();
                    throw new RestartResponseException(getSession().getPageFactory().newPage(
                            getApplication().getHomePage()));
                } else {
                    authenticationStrategy.remove();
                }
            }
        }

        super.onBeforeRender();
    }

    private boolean signIn(String email, String password) {
        return AuthenticatedWebSession.get().signIn(email, password);
    }

    private boolean isSignedIn() {
        return AuthenticatedWebSession.get().isSignedIn();
    }

    /**
     * Handle when a user fails to sign in.
     */
    protected void onSignInFailed() {
        error("Failed to Sign In");
    }

    /**
     * Handle when a user is signed in.
     */
    protected void onSignInSucceeded() {
        continueToOriginalDestination();
        setResponsePage(getApplication().getHomePage());
    }

    /**
     * Get a value indicating whether should remember the password or not.
     *
     * @return a value indicating whether should remember the password or not
     */
    public boolean isRememberMe() {
        return rememberMe;
    }

    /**
     * Set a value indicating whether should remember the password or not.
     *
     * @param rememberMe a value indicating whether should remember the password or not
     */
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    /**
     * Get the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get the email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the email address.
     *
     * @param email the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sign in form.
     */
    public final class SignInForm extends StatelessForm<PanelSignIn> {
        /**
         * Create a sign in form.
         *
         * @param id the markup ID of the form that is to be created
         */
        public SignInForm(String id) {
            super(id);

            setModel(new CompoundPropertyModel<>(PanelSignIn.this));

            add(new EmailTextField("email"));
            add(new PasswordTextField("password"));

            add(new PanelCaptcha("captcha"));

            add(new CheckBox("rememberMe"));
        }

        @Override
        public final void onSubmit() {
            IAuthenticationStrategy strategy = getApplication().getSecuritySettings()
                    .getAuthenticationStrategy();

            if (signIn(getEmail(), getPassword())) {
                if (rememberMe) {
                    strategy.save(email, password);
                } else {
                    strategy.remove();
                }

                onSignInSucceeded();
            } else {
                onSignInFailed();
                strategy.remove();
            }
        }
    }
}
