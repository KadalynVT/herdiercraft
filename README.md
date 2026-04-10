# Herdiercraft

Adds what I would consider missing features to the Herdcraft update (Minecraft's 2026 April Fools' Day update).

* Adds a new action: Alter / Rotate
* Allows you to place redstone dust & string (right-click with Punch or build action)
* Allows you to plant seeds onto farmland (right-click with Punch or build action)
* Allows you to use items on signs (right-click the item with Punch when it's near a built sign)
* Allows you to ride horses and most other mounts
  * Note that since animals can't be fed, the Nautilus can't be tamed in order to be ridden
* Living flowers will be placed in built flower pots when rolled on top of them.

## Alter / Rotate action

This tool allows for altering select block states, allowing you to actually build, especially redstone contraptions.

In general, left-click is "alter" and right-click is "rotate". If a block has a right-click action normally (including ones whose interfaces have been disabled) then you'll have to crouch + right-click.

Left-click actions:

* Toggle free-standing and wall-mounted blocks (toches, heads, signs)
* Toggle between half bed and full bed
* Toggle between half door and full door
* Toggle "attached" block state for hanging signs (the chain position)

Right-click actions:

* Rotate blocks toward the face you click
* For blocks which can't face up/down, clicking the top will rotate it clockwise
* For redstone dust and rails, it uses a 3x3 grid overlay on the block to detect how to change the block. For instance, if you click in the NW corner of the block, redstone dust will extend wires in those directions while rails will bend around that corner.
* Clicking a rail side when it's already straight and facing that way will cause the rail to ascend. If it's already ascended, it'll go back to flat.
* Clicking on a stair block uses a 2x2x2 overlay to determine which sub-cube to toggle. This is similar to Axiom's tinker tool but this will not allow you to change the block into any shape that's not a valid stair block.
* Clicking on a wall, fence, or glass pane will toggle the extension based on the face you click. Walls also allow toggling the "up" state if you click the top.
* Clicking the top face of a door will toggle its hinge.
* Slabs, trapdoors, and pointed dripstone will change which half they're on. Double slabs are not possible.
* Signs and heads will rotate clockwise
