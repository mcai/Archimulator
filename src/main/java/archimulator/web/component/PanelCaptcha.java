package archimulator.web.component;

import org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

/**
 * Captcha panel.
 *
 * @author Min Cai
 */
public class PanelCaptcha extends Panel {
    private final RequiredTextField<String> inputCaptcha;

    /**
     * Create a captcha panel.
     *
     * @param id the markup ID of the panel that is to be created
     */
    public PanelCaptcha(String id) {
        super(id);

        final CaptchaImageResource imageResource = new CaptchaImageResource(Model.of(String.format("%04d", (int) (Math.random() * 10000))));
        add(new Image("imageCaptcha", imageResource));

        inputCaptcha = new RequiredTextField<String>("captcha", new Model<String>(null)) {{
            add(new IValidator<String>() {
                @Override
                public void validate(IValidatable validatable) {
                    if (!imageResource.getChallengeIdModel().getObject().equals(inputCaptcha.getValue())) {
                        validatable.error(new ValidationError("Captcha is incorrect."));
                    }
                }
            });
        }};
        add(inputCaptcha);
    }
}
