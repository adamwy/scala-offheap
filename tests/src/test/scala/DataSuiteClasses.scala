package test

import scala.offheap._

@data class Point(x: Double, y: Double) {
  def distanceTo(other: Point): Double =
    math.sqrt(math.pow(other.x - x, 2) + math.pow(other.y - y, 2))
}

@data class A(b: B)
@data class B(a: A)
