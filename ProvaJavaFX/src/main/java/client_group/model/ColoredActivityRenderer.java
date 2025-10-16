package client_group.model;

import com.flexganttfx.model.ActivityRef;
import com.flexganttfx.model.layout.GanttLayout;
import com.flexganttfx.view.graphics.GraphicsBase;
import com.flexganttfx.view.graphics.renderer.ActivityBarRenderer;
import com.flexganttfx.view.graphics.renderer.ActivityRenderer;
import com.flexganttfx.view.util.Position;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import com.flexganttfx.view.*;
import javafx.scene.paint.Paint;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ColoredActivityRenderer extends ActivityBarRenderer<BlockActivity> {

    private Map<Long, Color> orderColorMap = new HashMap<>();

    public ColoredActivityRenderer(GraphicsBase<?> graphics) {
        super(graphics, "ColoredRenderer");
    }

    private Color getColorForOrderId(Long orderId) {
        if (orderId == null) {
            return Color.LIGHTGRAY; // default se non c'è orderId
        }
        if (!orderColorMap.containsKey(orderId)) {
            // genera un colore casuale o ciclico
            Color color = Color.hsb(Math.random() * 360, 0.6, 0.8);
            orderColorMap.put(orderId, color);
        }
        return orderColorMap.get(orderId);
    }

    @Override
    protected void drawBackground(ActivityRef<BlockActivity> activityRef, Position position, GraphicsContext gc,
                                  double x, double y, double w, double h,
                                  boolean selected, boolean hover, boolean highlighted, boolean pressed) {

        BlockActivity activity = activityRef.getActivity();
        Long orderId = null;
        if (activity != null && activity.getDTO() != null) {
            orderId = activity.getDTO().getOrderId();
        }

        Color baseColor = getColorForOrderId(orderId);

        Color fillColor = baseColor;
        if (selected) {
            fillColor = baseColor.brighter();
        } else if (hover) {
            fillColor = baseColor.deriveColor(0, 1, 1.2, 1);
        }

        gc.setFill(fillColor);
        gc.fillRoundRect(x, y, w, h, 5, 5);

        //super.drawBackground(activityRef, position, gc, x, y, w, h, selected, hover, highlighted, pressed);
    }

    @Override
    protected void drawBorder(ActivityRef<BlockActivity> activityRef, Position position, GraphicsContext gc,
                              double x, double y, double w, double h,
                              boolean selected, boolean hover, boolean highlighted, boolean pressed) {
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1.5);
        gc.strokeRect(x, y, w, h);


        //super.drawBorder(activityRef, position, gc, x, y, w, h, selected, hover, highlighted, pressed);
    }
}