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
package archimulator.web.page;

import archimulator.model.User;
import archimulator.service.ServiceManager;
import de.agilecoders.wicket.markup.html.bootstrap.common.NotificationPanel;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * Sign up page.
 *
 * @author Min Cai
 */
@MountPath(value = "/sign_up")
public class SignUpPage extends BasePage {
    /**
     * Create a sign up page.
     *
     * @param parameters the page parameters
     */
    public SignUpPage(final PageParameters parameters) {
        super(parameters);

        setTitle("Sign Up - Archimulator");

        add(new NotificationPanel("feedback"));

        add(new Form<Void>("signUp") {
            {
                final EmailTextField inputEmail = new EmailTextField("email", Model.of(" "));
                add(inputEmail);

                final PasswordTextField inputPassword = new PasswordTextField("password", Model.of(" "));
                add(inputPassword);

                PasswordTextField inputPassword2 = new PasswordTextField("password2", Model.of(" "));
                add(inputPassword2);

                add(new EqualPasswordInputValidator(inputPassword, inputPassword2));

                add(new AbstractFormValidator() {
                    @Override
                    public FormComponent<?>[] getDependentFormComponents() {
                        return new FormComponent<?>[]{inputEmail};
                    }

                    @Override
                    public void validate(Form<?> form) {
                        if (ServiceManager.getUserService().getUserByEmail(inputEmail.getInput()) != null) {
                            error(inputEmail, "user_with_same_id_exists");
                        }
                    }
                });

                add(new Button("submit") {
                    @Override
                    public void onSubmit() {
                        String email = inputEmail.getInput();
                        String password = inputPassword.getInput();

                        ServiceManager.getUserService().addUser(new User(email, password));
                        AuthenticatedWebSession.get().signIn(email, password);

                        setResponsePage(getApplication().getHomePage());
                    }
                });
            }
        });
    }
}
