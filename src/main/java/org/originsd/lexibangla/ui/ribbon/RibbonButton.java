package org.originsd.lexibangla.ui.ribbon;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Modern Ribbon button styled for the LexiBangla design system.
 */
public class RibbonButton extends Button {

    public enum Size {
        LARGE,
        SMALL
    }

    public RibbonButton(String text, Feather icon, Size size, Runnable action, String tooltipText) {
        super(text);
        getStyleClass().add("ribbon-button");
        getStyleClass().add(size == Size.LARGE ? "ribbon-button-large" : "ribbon-button-small");

        if (icon != null) {
            FontIcon fontIcon = new FontIcon(icon);
            fontIcon.setIconSize(size == Size.LARGE ? 22 : 16);
            fontIcon.getStyleClass().add("ribbon-icon");
            setGraphic(fontIcon);
        }

        if (size == Size.LARGE) {
            setContentDisplay(ContentDisplay.TOP);
            setAlignment(Pos.CENTER);
        } else {
            setContentDisplay(ContentDisplay.LEFT);
            setAlignment(Pos.CENTER_LEFT);
        }

        if (action != null) {
            setOnAction(e -> action.run());
        }

        if (tooltipText != null && !tooltipText.isBlank()) {
            setTooltip(new Tooltip(tooltipText));
        }

        setFocusTraversable(false);
    }

    public static RibbonButton large(String text, Feather icon, Runnable action, String tooltipText) {
        return new RibbonButton(text, icon, Size.LARGE, action, tooltipText);
    }

    public static RibbonButton small(String text, Feather icon, Runnable action, String tooltipText) {
        return new RibbonButton(text, icon, Size.SMALL, action, tooltipText);
    }
}
