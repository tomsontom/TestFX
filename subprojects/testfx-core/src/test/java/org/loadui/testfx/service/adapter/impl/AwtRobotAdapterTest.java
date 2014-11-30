/*
 * Copyright 2013-2014 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the Licence for the specific language governing permissions
 * and limitations under the Licence.
 */
package org.loadui.testfx.service.adapter.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.loadui.testfx.service.locator.PointLocator;
import org.loadui.testfx.service.locator.impl.BoundsLocatorImpl;
import org.loadui.testfx.service.locator.impl.PointLocatorImpl;
import org.testfx.api.FxLifecycle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.loadui.testfx.utils.WaitForAsyncUtils.asyncFx;
import static org.loadui.testfx.utils.WaitForAsyncUtils.sleep;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AwtRobotAdapterTest {

    //---------------------------------------------------------------------------------------------
    // FIELDS.
    //---------------------------------------------------------------------------------------------

    public AwtRobotAdapter robotAdapter;

    public Stage targetStage;
    public Parent sceneRoot;

    public Region region;
    public Point2D regionPoint;

    //---------------------------------------------------------------------------------------------
    // FIXTURE METHODS.
    //---------------------------------------------------------------------------------------------

    @BeforeClass
    public static void setupSpec() throws Exception {
        FxLifecycle.registerPrimaryStage();
    }

    @Before
    public void setup() throws Exception {
        robotAdapter = new AwtRobotAdapter();
        targetStage = FxLifecycle.setupStage(stage -> {
            region = new Region();
            region.setStyle("-fx-background-color: magenta;");

            VBox box = new VBox(region);
            box.setPadding(new Insets(10));
            box.setSpacing(10);
            VBox.setVgrow(region, Priority.ALWAYS);

            sceneRoot = new StackPane(box);
            Scene scene = new Scene(sceneRoot, 300, 100);
            stage.setScene(scene);
            stage.show();
        });

        PointLocator pointLocator = new PointLocatorImpl(new BoundsLocatorImpl());
        regionPoint = pointLocator.pointFor(region).atPosition(Pos.CENTER).query();
    }

    @After
    public void cleanup() {
        robotAdapter.keyRelease(KeyCode.A);
        robotAdapter.mouseRelease(MouseButton.PRIMARY);
    }

    //---------------------------------------------------------------------------------------------
    // FEATURE METHODS.
    //---------------------------------------------------------------------------------------------

    // ROBOT.

    @Test
    public void robotCreate() {
        // when:
        robotAdapter.robotCreate();

        // then:
        assertThat(robotAdapter.getRobotInstance(), notNullValue());
    }

    @Test
    public void robotDestroy_initialized_robot() {
        // given:
        robotAdapter.robotCreate();

        // when:
        robotAdapter.robotDestroy();

        // then:
        assertThat(robotAdapter.getRobotInstance(), nullValue());
    }

    @Test
    public void robotDestroy_uninitialized_robot() {
        // when:
        robotAdapter.robotDestroy();

        // then:
        assertThat(robotAdapter.getRobotInstance(), nullValue());
    }

    // KEY.

    @Test
    @SuppressWarnings("unchecked")
    public void keyPress() {
        // given:
        EventHandler<KeyEvent> keyEventHandler = mock(EventHandler.class);
        targetStage.addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);

        // and:
        robotAdapter.mouseMove(regionPoint);

        // when:
        robotAdapter.keyPress(KeyCode.A);

        // then:
        robotAdapter.timerWaitForIdle();
        verify(keyEventHandler, times(1)).handle(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void keyRelease() {
        // given:
        EventHandler<KeyEvent> keyEventHandler = mock(EventHandler.class);
        targetStage.addEventHandler(KeyEvent.KEY_RELEASED, keyEventHandler);

        // and:
        robotAdapter.mouseMove(regionPoint);

        // when:
        robotAdapter.keyPress(KeyCode.A);
        robotAdapter.keyRelease(KeyCode.A);

        // then:
        robotAdapter.timerWaitForIdle();
        verify(keyEventHandler, times(1)).handle(any());
    }

    // MOUSE.

    @Test
    public void getMouseLocation() {
        // when:
        Point2D mouseLocation = robotAdapter.getMouseLocation();

        // then:
        assertThat(mouseLocation.getX(), is(greaterThanOrEqualTo(0.0)));
        assertThat(mouseLocation.getY(), is(greaterThanOrEqualTo(0.0)));
    }

    @Test
    public void mouseMove() {
        // given:
        robotAdapter.mouseMove(new Point2D(100, 200));

        // when:
        robotAdapter.timerWaitForIdle();
        Point2D mouseLocation = robotAdapter.getMouseLocation();

        // then:
        assertThat(mouseLocation.getX(), is(100.0));
        assertThat(mouseLocation.getY(), is(200.0));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void mousePress() {
        // given:
        EventHandler<MouseEvent> mouseEventHandler = mock(EventHandler.class);
        region.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);

        // and:
        robotAdapter.mouseMove(regionPoint);

        // when:
        robotAdapter.mousePress(MouseButton.PRIMARY);

        // then:
        robotAdapter.timerWaitForIdle();
        verify(mouseEventHandler, times(1)).handle(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void mouseRelease() {
        // given:
        EventHandler<MouseEvent> mouseEventHandler = mock(EventHandler.class);
        region.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);

        // and:
        robotAdapter.mouseMove(regionPoint);

        // when:
        robotAdapter.mousePress(MouseButton.PRIMARY);
        robotAdapter.mouseRelease(MouseButton.PRIMARY);

        // then:
        robotAdapter.timerWaitForIdle();
        verify(mouseEventHandler, times(1)).handle(any());
    }

    // CAPTURE.

    @Test
    public void getCapturePixelColor() {
        // when:
        Color pixelColor = robotAdapter.getCapturePixelColor(regionPoint);

        // then:
        assertThat(pixelColor, is(Color.web("magenta")));
    }

    @Test
    public void getCaptureRegion() {
        // when:
        Rectangle2D region = new Rectangle2D(regionPoint.getX(), regionPoint.getY(), 10, 20);
        Image regionImage = robotAdapter.getCaptureRegion(region);

        // then:
        assertThat(regionImage.getWidth(), is(10.0));
        assertThat(regionImage.getHeight(), is(20.0));
        assertThat(regionImage.getPixelReader().getColor(5, 10), is(Color.web("magenta")));
    }

    // TIMER.

    @Test
    public void timerWaitForIdle() {
        // when:
        AtomicBoolean reachedStatement = new AtomicBoolean(false);
        asyncFx(() -> {
            sleep(100, TimeUnit.MILLISECONDS);
            asyncFx(() -> {
                reachedStatement.set(true);
            });
        });
        robotAdapter.timerWaitForIdle();

        // then:
        assertThat(reachedStatement.get(), is(true));
    }

}
