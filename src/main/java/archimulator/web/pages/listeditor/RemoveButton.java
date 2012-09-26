package archimulator.web.pages.listeditor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;

public class RemoveButton extends EditorButton {
    public RemoveButton(String id, Form<?> form) {
        super(id, form);
    }

    @Override
    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
        int idx = getItem().getIndex();

        for (int i = idx + 1; i < getItem().getParent().size(); i++) {
            ListItem<?> item = (ListItem<?>) getItem().getParent().get(i);
            item.setIndex(item.getIndex() - 1);
        }

        getList().remove(idx);
        getEditor().remove(getItem());

        if(target != null) {
            target.add(form);
        }
    }

    @Override
    public boolean isEnabled() {
        return getEditor().checkRemove(getItem());
    }
}
