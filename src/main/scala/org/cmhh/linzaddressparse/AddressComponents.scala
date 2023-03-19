package org.cmhh.linzaddressparse

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import scala.util.Random

/**
 * Address components.
 */
case class AddressComponents(
  unitType: Option[String],
  unitValue: Option[String],
  levelType: Option[String],
  levelValue: Option[String],
  addressNumber: Option[String],
  addressNumberSuffix: Option[String],
  addressNumberHigh: Option[String],
  roadName: Option[String],
  roadTypeName: Option[String],
  roadSuffix: Option[String],
  suburbLocality: Option[String],
  postcode: Option[String],
  townCity: Option[String]
) {
  private val r: Random = new Random()

  override lazy val toString: String = {
    streetaddr match {
      case None =>
        suburbLocality match {
          case None =>
            town match {
              case None => ""
              case Some(twn) => twn
            }
          case Some(suburb) =>
            town match {
              case None => suburb
              case Some(twn) => s"${suburb}, ${twn}"
            }
        }
      case Some(addr) =>
        suburbLocality match {
          case None =>
            town match {
              case None => addr
              case Some(twn) => s"${addr}, ${twn}"
            }
          case Some(suburb) =>
            town match {
              case None => s"${addr}, ${suburb}"
              case Some(twn) => s"${addr}, ${suburb}, ${twn}"
            }
        }
    }
  }

  lazy val toJson: String = 
    "{" +
    s"""\n  "unit_type":${s(unitType)},""" +
    s"""\n  "unit_value":${s(unitValue)},""" + 
    s"""\n  "level_type":${s(levelType)},""" +
    s"""\n  "level_value":${s(levelValue)},""" +
    s"""\n  "address_number":${s(addressNumber)},""" + 
    s"""\n  "address_number_suffix":${s(addressNumberSuffix)},""" + 
    s"""\n  "address_number_high":${s(addressNumberHigh)},""" + 
    s"""\n  "road_name":${s(roadName)},""" +
    s"""\n  "road_type_name":${s(roadTypeName)},""" + 
    s"""\n  "road_suffix":${s(roadSuffix)},""" +
    s"""\n  "suburb_locality":${s(suburbLocality)},""" +
    s"""\n  "town_city":${s(townCity)},""" +
    s"""\n  "postcode":${s(postcode)}""" +
    "\n}"

  /**
   * Assign a label to each character in address string.
   *
   * @param pDropUnit whether an address should be `Unit 1 10 Blah Street` or `1/10 Blah Street`.
   *
   * @return [[org.cmhh.linzaddressparse.Tagged]]
   */
  def labelledChars(pDropUnit: Double = 0): Tagged = {
    val no: Tagged = addressNumber match {
      case None => Tagged.empty
      case Some(no) =>
        Tagged(s"${no}".toVector, utils.rep("address_number", no.toVector.size))
    }

    val withUnit: Tagged = unitValue match {
      case None => no
      case Some(unit) =>
        unitValue match {
          case None => no
          case Some(unit) => 
            unitType match {
              case None =>
                Tagged(
                  s"${unit}/".toVector, utils.rep("unit_value", unit.size) ++ 
                    Vector("separator")
                ) ++ no
              case Some(utype) => 
                if (r.nextDouble() < pDropUnit) {
                  Tagged(
                    s"${utype} ${unit} ".toVector, 
                    utils.rep("unit_type", utype.size) ++ Vector("space") ++ 
                      utils.rep("unit_value", unit.size) ++ Vector("space")
                  ) ++ no
                } else {
                  Tagged(
                    s"${unit}/".toVector, utils.rep("unit_value", unit.size) ++ 
                      Vector("separator")
                  ) ++ no
                }
            }
        }
    }

    val withSuffix: Tagged = addressNumberSuffix match {
      case None => withUnit
      case Some(suffix) =>
        withUnit ++ Tagged(suffix.toVector, utils.rep("address_number_suffix", suffix.size))
    }

    val withLevel: Tagged = levelValue match {
      case None => withSuffix
      case Some(lvalue) =>
        levelType match {
          case None =>
            Tagged(
              s"level ${lvalue} ".toVector,
              utils.rep("level_type", "level".size) ++ Vector("space") ++ 
                utils.rep("level_value", lvalue.size)
            ) ++ withSuffix
          case Some(ltype) =>
            Tagged(
              s"${ltype} ${lvalue} ".toVector,
              utils.rep("level_type", ltype.size) ++ Vector("space") ++ 
                utils.rep("level_value", lvalue.size) ++ Vector("space")
            ) ++ withSuffix
        }
    }

    val withRoad: Tagged = roadName match {
      case None => withLevel
      case Some(rname) => 
        roadTypeName match {
          case None => 
            roadSuffix match {
              case None => 
                withLevel ++ 
                  Tagged(s" ${rname}".toVector, Vector("space") ++ utils.rep("road_name", rname.size))
              case Some(rsuffix) =>
                withLevel ++ 
                  Tagged(
                    s" ${rname} ${rsuffix}".toVector, 
                    Vector("space") ++ 
                      utils.rep("road_name", rname.size) ++ Vector("space") ++ 
                      utils.rep("road_suffix", rsuffix.size)
                  )
            }
          case Some(rtype) =>
            roadSuffix match {
              case None => 
                withLevel ++ 
                  Tagged(
                    s" ${rname} ${rtype}".toVector,
                    Vector("space") ++ 
                      utils.rep("road_name", rname.size) ++ Vector("space") ++ 
                      utils.rep("road_type_name", rtype.size)
                  )
              case Some(rsuffix) =>
                withLevel ++ 
                  Tagged(
                    s" ${rname} ${rtype} ${rsuffix}".toVector,
                    Vector("space") ++ 
                      utils.rep("road_name", rname.size) ++ Vector("space") ++ 
                      utils.rep("road_type_name", rtype.size) ++ Vector("space") ++ 
                      utils.rep("road_suffix", rsuffix.size)
                  )
            }
        }
    }

    val withSuburb: Tagged = suburbLocality match {
      case None => withRoad
      case Some(sub) =>
        withRoad ++ 
          Tagged(
            s", ${sub}".toVector,
            Vector("separator") ++ Vector("space") ++ 
              utils.rep("suburb_locality", sub.size)
          )
    }

    val withTown: Tagged = townCity match {
      case None => 
        postcode match {
          case None => 
            withSuburb
          case Some(pcode) =>
            withSuburb ++ 
              Tagged(
                s", $pcode".toVector,
                Vector("separator") ++ Vector("space") ++ 
                  utils.rep("postcode", pcode.size)
              )
        }
      case Some(town) => 
        postcode match {
          case None =>
            withSuburb ++ 
              Tagged(
                s", $town".toVector,
                Vector("separator") ++ Vector("space") ++ 
                  utils.rep("town_city", town.size)
              )
          case Some(pcode) =>
            withSuburb ++ 
              Tagged(
                s", $town $pcode".toVector,
                Vector("separator") ++ Vector("space") ++ 
                  utils.rep("town_city", town.size) ++ Vector("space") ++ 
                  utils.rep("postcode", pcode.size)
              )
        }
    }

    withTown
  }

  /**
   * Randomly pertub an address, returning a new address.
   *
   * @param pDropUnit probability unit type will be dropped
   * @param pSwapRoadType probability road type is replaced with an alias
   * @param pDropSuburb: probability suburb is dropped
   * @param pDropTownCity: probability town / city is dropped
   * @param pDropPostcode: probability postcode is dropped
   * @param pTypo: typo rate
   */
  def perturb(
    pDropUnit: Double = 0.1, 
    pSwapRoadType: Double = 0.5, 
    pDropSuburb: Double = 0.1, 
    pDropTownCity: Double = 0.1,
    pDropPostcode: Double = 0.1, 
    pTypo: Double = 0.05
  ): AddressComponents = {
    this
      .setUnitType(
        if (r.nextDouble() < pDropUnit) None else unitType
      )
      .setRoadTypeName(
        if (r.nextDouble() < pSwapRoadType) StreetTypeAlias.random(roadTypeName, true) 
        else utils.typo(roadTypeName, pTypo)
      )
      .setSuburbLocality(
        if (r.nextDouble() < pDropSuburb) None else utils.typo(suburbLocality, pTypo)
      )
      .setTownCity(
        if (r.nextDouble() < pDropTownCity) None else utils.typo(townCity, pTypo)
      )
      .setPostcode(
        if (r.nextDouble() < pDropPostcode) None else postcode
      )
  }

  def setUnitType(x: Option[String]): AddressComponents =  this.copy(unitType = x)
  def setUnitValue(x: Option[String]): AddressComponents = this.copy(unitValue = x)
  def setLevelValue(x: Option[String]): AddressComponents = this.copy(levelValue = x)
  def setAddressNumber(x: Option[String]): AddressComponents = this.copy(addressNumber = x)
  def setAddressNumberSuffix(x: Option[String]): AddressComponents = this.copy(addressNumberSuffix = x)
  def setAddressNumberHigh(x: Option[String]): AddressComponents = this.copy(addressNumberHigh = x)
  def setRoadName(x: Option[String]): AddressComponents = this.copy(roadName = x)
  def setRoadTypeName(x: Option[String]): AddressComponents = this.copy(roadTypeName = x)
  def setRoadSuffix(x: Option[String]): AddressComponents = this.copy(roadSuffix = x)
  def setSuburbLocality(x: Option[String]): AddressComponents = this.copy(suburbLocality = x)
  def setPostcode(x: Option[String]): AddressComponents = this.copy(postcode = x)
  def setTownCity(x: Option[String]): AddressComponents = this.copy(townCity = x)

  private lazy val houseNum: Option[String] = addressNumber match {
    case None => None
    case Some(no) =>
      addressNumberSuffix match {
        case None => Some(no)
        case Some(suffix) => Some(s"${no}${suffix}")
      }
  }

  private lazy val houseNumWithUnit: Option[String] = houseNum match {
    case None => None
    case Some(no) =>
      unitValue match {
        case None => Some(no)
        case Some(uval) =>
          unitType match {
            case None => Some(s"${uval}/${no}")
            case Some(utype) => Some(s"${utype} ${uval} ${no}")
          }
      }
  }

  private lazy val streetno: Option[String] = houseNumWithUnit match {
    case None => None
    case Some(no) =>
      levelValue match {
        case None => Some(no)
        case Some(lvalue) => 
          levelType match {
            case None => Some(s"LEVEL ${lvalue} ${no}")
            case Some(ltype) => Some(s"${ltype} ${lvalue} ${no}")
          }
      }
  }

  private lazy val road: Option[String] = roadName match {
    case None => None
    case Some(rd) => 
      roadTypeName match {
        case None =>
          roadSuffix match {
            case None => Some(rd)
            case Some(suffix) => Some(s"${rd} ${suffix}")
          }
        case Some(rtype) => 
          roadSuffix match {
            case None => Some(s"${rd} ${rtype}")
            case Some(suffix) => Some(s"${rd} ${rtype} ${suffix}")
          }
      }
  }

  private lazy val streetaddr: Option[String] = road match {
    case None => None
    case Some(rd) => 
      streetno match {
        case None => Some(rd)
        case Some(no) => Some(s"${no} ${rd}")
      }
  }

  private lazy val town: Option[String] = townCity match {
    case None => None
    case Some(twn) => 
      postcode match {
        case None => Some(twn)
        case Some(p) => Some(s"${twn} ${p}")
      }
  }

  private def s(x: Option[String]): String = x match {
    case Some(v) => s""""$v""""
    case None => "null"
  } 
}

/**
 * As close as will ever get to that awesome java builder pattern :)
 */
object AddressComponents {
  def apply(): AddressComponents = AddressComponents(
    None, None, None, None, None, None, None, None, None, None, None, None, None
  )
}