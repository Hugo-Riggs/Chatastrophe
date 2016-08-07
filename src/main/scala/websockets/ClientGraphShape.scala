package websockets

case class ClientGraphShape[In, Out](
                               fromServerWebsocket: Inlet[In],
                               toServerWebsocket: Outlet[Out]
                               ) extends Shape {
  override val inlets: immutable.Seq[Inlet[_]] =
    fromServerWebsocket :: Nil
  override val outlets: immutable.Seq[Outlet[_]] =
    toServerWebsocket :: Nil

  override def deepCopy() = ClientGraphShape(
    fromServerWebsocket.carbonCopy(),
    toServerWebsocket.carbonCopy()
  )

  override def copyFromPorts(
    inlets:  immutable.Seq[Inlet[_]],
    outlets: immutable.Seq[Outlet[_]]) = {
      assert(inlets.size == this.inlets.size)
      assert(outlets.size == this.outlets.size)
      ClientGraphShape[In, Out](inlets(0).as[In], outlets(0).as[Out])
  }
}