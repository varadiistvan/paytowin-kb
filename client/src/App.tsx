import { useEffect, useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import { PayToWinClient } from './paytowin.client'
import { GrpcWebFetchTransport } from '@protobuf-ts/grpcweb-transport'
import { PlayersRequest } from './paytowin'


function App() {
  const [count, setCount] = useState(0)

  useEffect(() => {
    let client = new PayToWinClient(new GrpcWebFetchTransport({
      baseUrl: "http://localhost:50051"
    }))
    let a = client.getPlayers(PlayersRequest.create());

    (async () => {
      for await (let players of a.responses) {
        console.log(players)
      }
    })()

    
  }, [])


  return (
    <>
      <div>
        <a href="https://vitejs.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>
      <h1>Vite + React</h1>
      <div className="card">
        <button onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </button>
        <p>
          Edit <code>src/App.tsx</code> and save to test HMR
        </p>
      </div>
      <p className="read-the-docs">
        Click on the Vite and React logos to learn more
      </p>
    </>
  )
}

export default App
