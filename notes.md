## hotbar
### net/minecraft/client/gui/Gui.java

extractItemHotbar adjust offsets for bottom bar

```diff
597,598c597,598
<     graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, screenCenter - 91, graphics.guiHeight() - 22, 182, 22);
<     graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_SPRITE, screenCenter - 91 - 1 + player.getInventory().getSelectedSlot() * 20, graphics.guiHeight() - 22 - 1, 24, 23);
---
>     graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, screenCenter - 91 + 10, graphics.guiHeight() - 22, 182, 22);
>     graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_SPRITE, screenCenter - 91 + 10 - 1 + player.getInventory().getSelectedSlot() * 20, graphics.guiHeight() - 22 - 1, 24, 23);
606,607c606,607
<     for (int i = 0; i < 9; i++) {
<       int x = screenCenter - 90 + i * 20 + 2;
---
>     for (int i = 0; i < 8; i++) {
>       int x = screenCenter - 80 + i * 20 + 2;
```

### net/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen.java

and $ItemPickerMenu, removed 9th slot

```java
addInventoryHotbarSlots((Container)inventory, 9, 112);
```

### net/minecraft/client/player/inventory/Hotbar.java

private static final int SIZE = Inventory.getSelectionSize();

## rotate action

things to move

* any: face, facing, north, west, east, south
  * face: lever, 
* torches: change between regular and wall torch
* trapdoors: half
* slabs: type* (don't allow double)
* stairs: shape?
* fence gates: in_wall?
* wall: up
* chains: axis
* beds and doors: make other part or toggle thru the three options (show bottom, show top, show both)
* heads & signs: rotation, change between regular and wall
* hanging signs: rotation, attached
* pointed dripstone: vertical_direction

## things to do

- [x] rotate action
- [x] place redstone dust
- [ ] books sucked into lecturns
- [ ] flowers sucked into flower pots
- [ ] glowstone stucked into respawn anchors
- [x] mounting horses
- [ ] building dripstone connects it to other dripstone
- [x] apply wax (right click wax)
- [ ] planting seeds?
