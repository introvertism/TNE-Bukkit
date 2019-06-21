package net.tnemc.signs.signs.impl;

import net.tnemc.signs.signs.SignStep;
import net.tnemc.signs.signs.SignType;
import net.tnemc.signs.signs.impl.command.CommandSelectionStep;
import net.tnemc.signs.signs.impl.command.CostSelectionStep;

import java.util.HashMap;
import java.util.Map;

/**
 * The New Economy Minecraft Server Plugin
 * <p>
 * Created by creatorfromhell on 6/18/2019.
 * <p>
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 * Created by creatorfromhell on 06/30/2017.
 */
public class CommandSign implements SignType {
  /**
   * @return The name of this sign type.
   */
  @Override
  public String name() {
    return "command";
  }

  /**
   * @return The permission node required to use this sign.
   */
  @Override
  public String usePermission() {
    return "tne.sign.command.use";
  }

  /**
   * @return The permission node required to create this sign.
   */
  @Override
  public String createPermission() {
    return "tne.sign.command.create";
  }

  @Override
  public Map<Integer, SignStep> steps() {
    Map<Integer, SignStep> steps = new HashMap<>();
    steps.put(1, new CommandSelectionStep());
    steps.put(2, new CostSelectionStep());
    return steps;
  }
}