/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.animation;

import static com.facebook.litho.animation.AnimatedProperties.SCALE;
import static com.facebook.litho.dataflow.GraphBinding.create;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

import android.view.View;
import com.facebook.litho.OutputUnitType;
import com.facebook.litho.OutputUnitsAffinityGroup;
import com.facebook.litho.dataflow.DataFlowGraph;
import com.facebook.litho.dataflow.GraphBinding;
import com.facebook.litho.dataflow.MockTimingSource;
import com.facebook.litho.dataflow.OutputOnlyNode;
import com.facebook.litho.dataflow.SettableNode;
import com.facebook.litho.dataflow.SimpleNode;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ComponentsTestRunner.class)
public class AnimatedPropertyNodeTest {

  private MockTimingSource mTestTimingSource;
  private DataFlowGraph mDataFlowGraph;

  @Before
  public void setUp() throws Exception {
    mTestTimingSource = new MockTimingSource();
    mDataFlowGraph = DataFlowGraph.create(mTestTimingSource);
  }

  @Test
  public void testViewPropertyNodeWithInput() {
    View view = new View(application);
    OutputUnitsAffinityGroup<Object> group = new OutputUnitsAffinityGroup<>();
    group.add(OutputUnitType.HOST, view);
    SettableNode source = new SettableNode();
    SimpleNode middle = new SimpleNode();
    AnimatedPropertyNode destination = new AnimatedPropertyNode(group, SCALE);

    GraphBinding binding = create(mDataFlowGraph);
    binding.addBinding(source, middle);
    binding.addBinding(middle, destination);
    binding.activate();

    mTestTimingSource.step(1);

    assertThat(view.getScaleX()).isEqualTo(0f);

    source.setValue(37);
    mTestTimingSource.step(1);

    assertThat(view.getScaleX()).isEqualTo(37f);
  }

  @Test
  public void testViewPropertyNodeWithInputAndOutput() {
    View view = new View(application);
    OutputUnitsAffinityGroup<Object> group = new OutputUnitsAffinityGroup<>();
    group.add(OutputUnitType.HOST, view);
    SettableNode source = new SettableNode();
    AnimatedPropertyNode animatedNode = new AnimatedPropertyNode(group, SCALE);
    OutputOnlyNode destination = new OutputOnlyNode();

    GraphBinding binding = create(mDataFlowGraph);
    binding.addBinding(source, animatedNode);
    binding.addBinding(animatedNode, destination);
    binding.activate();

    mTestTimingSource.step(1);

    assertThat(view.getScaleX()).isEqualTo(0f);
    assertThat(destination.getValue()).isEqualTo(0f);

    source.setValue(123);
    mTestTimingSource.step(1);

    assertThat(view.getScaleX()).isEqualTo(123f);
    assertThat(destination.getValue()).isEqualTo(123f);
  }

  @Test
  public void testSettingMountContentOnNodeWithValue() {
    View view1 = new View(application);
    OutputUnitsAffinityGroup<Object> group1 = new OutputUnitsAffinityGroup<>();
    group1.add(OutputUnitType.HOST, view1);

    View view2 = new View(application);
    OutputUnitsAffinityGroup<Object> group2 = new OutputUnitsAffinityGroup<>();
    group2.add(OutputUnitType.HOST, view2);

    SettableNode source = new SettableNode();
    AnimatedPropertyNode animatedNode = new AnimatedPropertyNode(group1, SCALE);

    GraphBinding binding = create(mDataFlowGraph);
    binding.addBinding(source, animatedNode);
    binding.activate();

    mTestTimingSource.step(1);

    assertThat(view1.getScaleX()).isEqualTo(0f);

    source.setValue(123);
    mTestTimingSource.step(1);

    assertThat(view1.getScaleX()).isEqualTo(123f);

    assertThat(view2.getScaleX()).isEqualTo(1f);

    animatedNode.setMountContentGroup(group2);

    assertThat(view2.getScaleX()).isEqualTo(123f);
  }
}
