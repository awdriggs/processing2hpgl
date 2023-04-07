# processing2hpgl

An HPGL and Processing mash-up. Allows for a direct connection between Processing and HP compatible Pen Plotters. The goal of this project is to match processing drawing commands to HPGL commands, giving you direct control of the printer from Processing.

## Resources 
- Hewlett Packard Graphics Language [HPGL Reference](https://www.isoplotec.co.jp/HPGL/eHPGL.htm#Table%20of%20contents)

## Tested Printers
### HP7475A
[Manual](https://pearl-hifi.com/06_Lit_Archive/15_Mfrs_Publications/20_HP_Agilent/HP_7475A_Plotter/HP_7475A_Op_Interconnect.pdf)
* HPGL1 Doesn't support polygon fills

## Notes
- The Processing coordinate system and the HPGL coordinates don't match. In processing, (0,0) is the upper left corner while in HPGL it is the lower left corner.  
- Library makes the assumption that all angles are expressed in radians
- Currently, no option to draw ellipses. Is is possible with HPGL but requires some use of the scale command. See this [forum thread](https://h30434.www3.hp.com/t5/Printers-Archive-Read-Only/draw-ellipse-or-oval-with-hp-gl-2/td-p/4519666)
- Likewise, Arcs cannot be elliptical.

## Acknowledgements
- Thanks to Tobias Toft, I used (his example)[https://github.com/tobiastoft/SymbolicDisarray) as a starting point for the plotter class.
 
  
