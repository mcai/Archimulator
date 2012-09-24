/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.web.components;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;

public class SignInPanel extends Panel {
    private boolean includeRememberMe = true;
    private boolean rememberMe = true;
    private String email;
    private String password;

    public SignInPanel(String id) {
        this(id, true);
    }

    public SignInPanel(String id, boolean includeRememberMe) {
        super(id);

        this.includeRememberMe = includeRememberMe;

        add(new FeedbackPanel("span_feedback"));
        add(new SignInForm("form_sign_in"));
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

    protected void onSignInFailed() {
        error("Failed to Sign In");
    }

    protected void onSignInSucceeded() {
        continueToOriginalDestination();
        setResponsePage(getApplication().getHomePage());
    }

    public boolean isIncludeRememberMe() {
        return includeRememberMe;
    }

    public void setIncludeRememberMe(boolean includeRememberMe) {
        this.includeRememberMe = includeRememberMe;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public final class SignInForm extends StatelessForm<SignInPanel> {
        public SignInForm(String id) {
            super(id);

            setModel(new CompoundPropertyModel<SignInPanel>(SignInPanel.this));

            add(new EmailTextField("input_email", new PropertyModel<String>(SignInPanel.this, "email")));
            add(new PasswordTextField("input_password", new PropertyModel<String>(SignInPanel.this, "password")));

            WebMarkupContainer divRememberMe = new WebMarkupContainer("div_remember_me");
            add(divRememberMe);

            divRememberMe.add(new CheckBox("checkbox_remember_me", new PropertyModel<Boolean>(SignInPanel.this, "rememberMe")));
            divRememberMe.setVisible(includeRememberMe);
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
